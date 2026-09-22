package com.glassfinder.service;

import com.glassfinder.dto.AddStockRequest;
import com.glassfinder.dto.BoxResponse;
import com.glassfinder.dto.CreateBoxRequest;
import com.glassfinder.dto.SellStockRequest;
import com.glassfinder.dto.StockHistoryResponse;
import com.glassfinder.dto.StockResponse;
import com.glassfinder.entity.GlassBox;
import com.glassfinder.entity.GlassCompatibility;
import com.glassfinder.entity.PhoneModel;
import com.glassfinder.entity.StockTransaction;
import com.glassfinder.exception.BoxNotFoundException;
import com.glassfinder.exception.DuplicateBoxException;
import com.glassfinder.exception.InsufficientStockException;
import com.glassfinder.repository.GlassBoxRepository;
import com.glassfinder.repository.GlassCompatibilityRepository;
import com.glassfinder.repository.PhoneModelRepository;
import com.glassfinder.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BoxService {

    private final GlassBoxRepository boxRepository;
    private final PhoneModelRepository phoneModelRepository;
    private final GlassCompatibilityRepository compatibilityRepository;
    private final StockTransactionRepository transactionRepository;

    public BoxService(
            GlassBoxRepository boxRepository,
            PhoneModelRepository phoneModelRepository,
            GlassCompatibilityRepository compatibilityRepository,
            StockTransactionRepository transactionRepository
    ) {
        this.boxRepository = boxRepository;
        this.phoneModelRepository = phoneModelRepository;
        this.compatibilityRepository = compatibilityRepository;
        this.transactionRepository = transactionRepository;
    }

    // ==========================================
    // CREATE BOX
    // ==========================================

    @Transactional
    public BoxResponse createBox(CreateBoxRequest request) {

        String boxCode = request.boxCode().trim();

        if (boxRepository.findByBoxCode(boxCode).isPresent()) {
            throw new DuplicateBoxException(
                    "Box code already exists: " + boxCode
            );
        }

        LocalDateTime now = LocalDateTime.now();

        GlassBox box = new GlassBox();

        box.setBoxCode(boxCode);
        box.setCreatedAt(now);
        box.setUpdatedAt(now);

        box = boxRepository.save(box);

        for (String modelName : request.models()) {

            String cleanedModelName =
                    modelName.trim().toLowerCase();

            PhoneModel model = phoneModelRepository
                    .findByModelNameIgnoreCase(cleanedModelName)
                    .orElseGet(() -> {

                        PhoneModel newModel = new PhoneModel();

                        newModel.setModelName(cleanedModelName);
                        newModel.setCreatedAt(now);

                        return phoneModelRepository.save(newModel);
                    });

            GlassCompatibility compatibility =
                    new GlassCompatibility();

            compatibility.setBox(box);
            compatibility.setModel(model);
            compatibility.setCreatedAt(now);

            compatibilityRepository.save(compatibility);
        }

        StockTransaction transaction =
                new StockTransaction();

        transaction.setBox(box);
        transaction.setTransactionType(
                StockTransaction.TransactionType.ADD
        );
        transaction.setQuantity(request.quantity());
        transaction.setNotes("Initial stock");
        transaction.setCreatedAt(now);

        transactionRepository.save(transaction);

        return convertToResponse(box);
    }


    // ==========================================
    // GET ALL BOXES
    // ==========================================

    @Transactional(readOnly = true)
    public List<BoxResponse> getAllBoxes() {

        return boxRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    // ==========================================
    // SEARCH BY PHONE MODEL
    // ==========================================

    @Transactional(readOnly = true)
    public List<BoxResponse> searchByModel(String modelName) {

        List<GlassCompatibility> compatibilityList =
                compatibilityRepository
                        .findByModel_ModelNameIgnoreCase(
                                modelName.trim()
                        );

        return compatibilityList
                .stream()
                .map(GlassCompatibility::getBox)
                .distinct()
                .map(this::convertToResponse)
                .filter(box -> box.currentStock() > 0)
                .toList();
    }


    // ==========================================
    // SELL STOCK
    // ==========================================

    @Transactional
    public StockResponse sellStock(SellStockRequest request) {

        String boxCode = request.boxCode().trim();

        GlassBox box = findBox(boxCode);

        Long currentStock =
                transactionRepository.calculateCurrentStock(box);

        if (request.quantity() > currentStock) {

            throw new InsufficientStockException(
                    "Insufficient stock. Available: "
                            + currentStock
                            + ", requested: "
                            + request.quantity()
            );
        }

        LocalDateTime now = LocalDateTime.now();

        StockTransaction transaction =
                new StockTransaction();

        transaction.setBox(box);

        transaction.setTransactionType(
                StockTransaction.TransactionType.SOLD
        );

        transaction.setQuantity(request.quantity());

        transaction.setNotes(
                request.notes() != null
                        ? request.notes()
                        : "Stock sold"
        );

        transaction.setCreatedAt(now);

        transactionRepository.save(transaction);

        box.setUpdatedAt(now);
        boxRepository.save(box);

        Long remainingStock =
                transactionRepository.calculateCurrentStock(box);

        return new StockResponse(
                box.getBoxCode(),
                "SOLD",
                request.quantity(),
                remainingStock,
                "Stock sold successfully"
        );
    }


    // ==========================================
    // ADD STOCK
    // ==========================================

    @Transactional
    public StockResponse addStock(AddStockRequest request) {

        String boxCode = request.boxCode().trim();

        GlassBox box = findBox(boxCode);

        LocalDateTime now = LocalDateTime.now();

        StockTransaction transaction =
                new StockTransaction();

        transaction.setBox(box);

        transaction.setTransactionType(
                StockTransaction.TransactionType.ADD
        );

        transaction.setQuantity(request.quantity());

        transaction.setNotes(
                request.notes() != null
                        ? request.notes()
                        : "Stock added"
        );

        transaction.setCreatedAt(now);

        transactionRepository.save(transaction);

        box.setUpdatedAt(now);
        boxRepository.save(box);

        Long currentStock =
                transactionRepository.calculateCurrentStock(box);

        return new StockResponse(
                box.getBoxCode(),
                "ADD",
                request.quantity(),
                currentStock,
                "Stock added successfully"
        );
    }


    // ==========================================
    // STOCK HISTORY
    // ==========================================

    @Transactional(readOnly = true)
    public List<StockHistoryResponse> getStockHistory(
            String boxCode
    ) {

        GlassBox box = findBox(boxCode);

        List<StockTransaction> transactions =
                transactionRepository
                        .findByBoxOrderByCreatedAtDesc(box);

        /*
         * We calculate stock after each transaction.
         *
         * Transactions are returned newest first,
         * so calculate from oldest → newest first.
         */

        List<StockTransaction> chronological =
                new ArrayList<>(transactions);

        java.util.Collections.reverse(chronological);

        long runningStock = 0;

        List<StockHistoryResponse> history =
                new ArrayList<>();

        for (StockTransaction transaction : chronological) {

            if (transaction.getTransactionType()
                    == StockTransaction.TransactionType.ADD) {

                runningStock += transaction.getQuantity();

            } else if (transaction.getTransactionType()
                    == StockTransaction.TransactionType.SOLD) {

                runningStock -= transaction.getQuantity();
            }

            history.add(
                    new StockHistoryResponse(
                            transaction.getId(),
                            box.getBoxCode(),
                            transaction
                                    .getTransactionType()
                                    .name(),
                            transaction.getQuantity(),
                            transaction.getNotes(),
                            transaction.getCreatedAt(),
                            runningStock
                    )
            );
        }

        // Return newest first
        java.util.Collections.reverse(history);

        return history;
    }


    // ==========================================
    // FIND BOX
    // ==========================================

    private GlassBox findBox(String boxCode) {

        return boxRepository
                .findByBoxCode(boxCode)
                .orElseThrow(() ->
                        new BoxNotFoundException(
                                "Box not found: " + boxCode
                        )
                );
    }


    // ==========================================
    // ENTITY → DTO
    // ==========================================

    private BoxResponse convertToResponse(GlassBox box) {

        List<String> models =
                compatibilityRepository
                        .findByBox_Id(box.getId())
                        .stream()
                        .map(compatibility ->
                                compatibility
                                        .getModel()
                                        .getModelName()
                        )
                        .toList();

        Long currentStock =
                transactionRepository
                        .calculateCurrentStock(box);

        return new BoxResponse(
                box.getId(),
                box.getBoxCode(),
                models,
                currentStock,
                box.getCreatedAt(),
                box.getUpdatedAt()
        );
    }
}