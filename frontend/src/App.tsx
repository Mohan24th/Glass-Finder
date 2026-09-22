import { useEffect, useState } from "react";
import {
  addStock,
  createBox,
  getBoxes,
  getHistory,
  searchBoxes,
  sellStock,
  type Box,
  type StockHistory,
} from "./api";

function App() {
  const [boxes, setBoxes] = useState<Box[]>([]);

  const [search, setSearch] = useState("");

  const [boxCode, setBoxCode] = useState("");
  const [models, setModels] = useState("");
  const [quantity, setQuantity] = useState("");

  const [stockBox, setStockBox] = useState("");
  const [stockQuantity, setStockQuantity] = useState("");

  const [history, setHistory] = useState<StockHistory[]>([]);
  const [historyBox, setHistoryBox] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadBoxes = async () => {
    try {
      setError("");
      const data = await getBoxes();
      setBoxes(data);
    } catch {
      setError("Cannot connect to backend. Is Spring Boot running?");
    }
  };

  useEffect(() => {
    loadBoxes();
  }, []);

  const handleSearch = async () => {
    try {
      setError("");
      setMessage("");

      if (!search.trim()) {
        await loadBoxes();
        return;
      }

      const data = await searchBoxes(search);
      setBoxes(data);
    } catch (err: any) {
      setError(
          err.response?.data?.message || "Search failed"
      );
    }
  };

  const handleCreateBox = async () => {
    try {
      setError("");
      setMessage("");

      if (!boxCode.trim() || !models.trim() || !quantity) {
        setError("Please fill all box fields");
        return;
      }

      const modelList = models
          .split(",")
          .map((model) => model.trim())
          .filter(Boolean);

      const data = await createBox({
        boxCode: boxCode.trim(),
        models: modelList,
        quantity: Number(quantity),
      });

      setMessage(`${data.boxCode} created successfully`);

      setBoxCode("");
      setModels("");
      setQuantity("");

      await loadBoxes();
    } catch (err: any) {
      setError(
          err.response?.data?.message ||
          "Failed to create box"
      );
    }
  };

  const handleAddStock = async () => {
    try {
      setError("");
      setMessage("");

      if (!stockBox.trim() || !stockQuantity) {
        setError("Enter box code and quantity");
        return;
      }

      const data = await addStock({
        boxCode: stockBox.trim(),
        quantity: Number(stockQuantity),
        notes: "Stock added from frontend",
      });

      setMessage(
          `${data.boxCode}: stock is now ${data.currentStock}`
      );

      setStockBox("");
      setStockQuantity("");

      await loadBoxes();
    } catch (err: any) {
      setError(
          err.response?.data?.message ||
          "Failed to add stock"
      );
    }
  };

  const handleSellStock = async () => {
    try {
      setError("");
      setMessage("");

      if (!stockBox.trim() || !stockQuantity) {
        setError("Enter box code and quantity");
        return;
      }

      const data = await sellStock({
        boxCode: stockBox.trim(),
        quantity: Number(stockQuantity),
        notes: "Customer purchase",
      });

      setMessage(
          `${data.boxCode}: ${data.currentStock} units remaining`
      );

      setStockBox("");
      setStockQuantity("");

      await loadBoxes();
    } catch (err: any) {
      setError(
          err.response?.data?.message ||
          "Failed to sell stock"
      );
    }
  };

  const handleHistory = async (code: string) => {
    try {
      setError("");

      const data = await getHistory(code);

      setHistory(data);
      setHistoryBox(code);
    } catch (err: any) {
      setError(
          err.response?.data?.message ||
          "Failed to load history"
      );
    }
  };

  const totalStock = boxes.reduce(
      (sum, box) => sum + box.currentStock,
      0
  );

  const lowStock = boxes.filter(
      (box) => box.currentStock < 3
  ).length;

  return (
      <div className="app">
        <header className="header">
          <div>
            <h1>Glass Finder</h1>
            <p>Mobile Glass Inventory Management</p>
          </div>

          <button
              className="secondary-button"
              onClick={loadBoxes}
          >
            Refresh
          </button>
        </header>

        <main className="container">

          {/* Alerts */}

          {message && (
              <div className="alert success">
                {message}
              </div>
          )}

          {error && (
              <div className="alert error">
                {error}
              </div>
          )}

          {/* Statistics */}

          <section className="stats">

            <div className="stat-card">
              <span>Total Boxes</span>
              <strong>{boxes.length}</strong>
            </div>

            <div className="stat-card">
              <span>Total Stock</span>
              <strong>{totalStock}</strong>
            </div>

            <div className="stat-card warning">
              <span>Low Stock</span>
              <strong>{lowStock}</strong>
            </div>

          </section>

          {/* Search */}

          <section className="card">

            <h2>Search Glass by Phone Model</h2>

            <div className="search-row">

              <input
                  type="text"
                  placeholder="Example: v20"
                  value={search}
                  onChange={(e) =>
                      setSearch(e.target.value)
                  }
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleSearch();
                    }
                  }}
              />

              <button onClick={handleSearch}>
                Search
              </button>

              <button
                  className="secondary-button"
                  onClick={loadBoxes}
              >
                Clear
              </button>

            </div>

          </section>

          {/* Create Box */}

          <section className="card">

            <h2>Add New Glass Box</h2>

            <div className="form-grid">

              <div>
                <label>Box Code</label>

                <input
                    placeholder="BOX005"
                    value={boxCode}
                    onChange={(e) =>
                        setBoxCode(e.target.value)
                    }
                />
              </div>

              <div>
                <label>Compatible Models</label>

                <input
                    placeholder="v20, rm20i, rm30"
                    value={models}
                    onChange={(e) =>
                        setModels(e.target.value)
                    }
                />
              </div>

              <div>
                <label>Initial Quantity</label>

                <input
                    type="number"
                    min="1"
                    placeholder="10"
                    value={quantity}
                    onChange={(e) =>
                        setQuantity(e.target.value)
                    }
                />
              </div>

            </div>

            <button onClick={handleCreateBox}>
              Create Box
            </button>

          </section>

          {/* Stock Management */}

          <section className="card">

            <h2>Stock Management</h2>

            <div className="form-grid">

              <div>
                <label>Box Code</label>

                <input
                    placeholder="BOX001"
                    value={stockBox}
                    onChange={(e) =>
                        setStockBox(e.target.value)
                    }
                />
              </div>

              <div>
                <label>Quantity</label>

                <input
                    type="number"
                    min="1"
                    placeholder="2"
                    value={stockQuantity}
                    onChange={(e) =>
                        setStockQuantity(e.target.value)
                    }
                />
              </div>

            </div>

            <div className="button-row">

              <button onClick={handleAddStock}>
                + Add Stock
              </button>

              <button
                  className="danger-button"
                  onClick={handleSellStock}
              >
                Sell Stock
              </button>

            </div>

          </section>

          {/* Inventory */}

          <section className="card">

            <div className="section-header">

              <div>
                <h2>Inventory</h2>
                <p>
                  Current available glass stock
                </p>
              </div>

            </div>

            <div className="table-wrapper">

              <table>

                <thead>
                <tr>
                  <th>Box</th>
                  <th>Compatible Models</th>
                  <th>Stock</th>
                  <th>Status</th>
                  <th>History</th>
                </tr>
                </thead>

                <tbody>

                {boxes.length === 0 ? (
                    <tr>
                      <td
                          colSpan={5}
                          className="empty"
                      >
                        No boxes found
                      </td>
                    </tr>
                ) : (
                    boxes.map((box) => (

                        <tr key={box.id}>

                          <td>
                            <strong>
                              {box.boxCode}
                            </strong>
                          </td>

                          <td>
                            <div className="tags">
                              {box.models.map(
                                  (model) => (
                                      <span
                                          className="tag"
                                          key={model}
                                      >
                                {model}
                              </span>
                                  )
                              )}
                            </div>
                          </td>

                          <td>
                            <strong>
                              {box.currentStock}
                            </strong>
                          </td>

                          <td>

                            {box.currentStock === 0 ? (
                                <span className="status out">
                            Out of Stock
                          </span>
                            ) : box.currentStock < 3 ? (
                                <span className="status low">
                            Low Stock
                          </span>
                            ) : (
                                <span className="status good">
                            Available
                          </span>
                            )}

                          </td>

                          <td>

                            <button
                                className="small-button"
                                onClick={() =>
                                    handleHistory(
                                        box.boxCode
                                    )
                                }
                            >
                              View
                            </button>

                          </td>

                        </tr>

                    ))
                )}

                </tbody>

              </table>

            </div>

          </section>

          {/* History */}

          {historyBox && (

              <section className="card">

                <div className="section-header">

                  <div>
                    <h2>
                      Stock History — {historyBox}
                    </h2>

                    <p>
                      Immutable stock transactions
                    </p>
                  </div>

                  <button
                      className="secondary-button"
                      onClick={() => {
                        setHistory([]);
                        setHistoryBox("");
                      }}
                  >
                    Close
                  </button>

                </div>

                <div className="table-wrapper">

                  <table>

                    <thead>
                    <tr>
                      <th>Type</th>
                      <th>Quantity</th>
                      <th>Stock After</th>
                      <th>Notes</th>
                      <th>Date</th>
                    </tr>
                    </thead>

                    <tbody>

                    {history.map((item) => (

                        <tr key={item.id}>

                          <td>
                        <span
                            className={
                              item.transactionType ===
                              "SOLD"
                                  ? "status out"
                                  : "status good"
                            }
                        >
                          {item.transactionType}
                        </span>
                          </td>

                          <td>
                            {item.quantity}
                          </td>

                          <td>
                            <strong>
                              {item.stockAfterTransaction}
                            </strong>
                          </td>

                          <td>
                            {item.notes}
                          </td>

                          <td>
                            {new Date(
                                item.createdAt
                            ).toLocaleString()}
                          </td>

                        </tr>

                    ))}

                    </tbody>

                  </table>

                </div>

              </section>

          )}

        </main>
      </div>
  );
}

export default App;