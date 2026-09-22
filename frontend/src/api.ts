import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api",
});

export interface Box {
    id: number;
    boxCode: string;
    models: string[];
    currentStock: number;
    createdAt: string;
    updatedAt: string;
}

export interface StockResponse {
    boxCode: string;
    transactionType: string;
    quantity: number;
    currentStock: number;
    message: string;
}

export interface StockHistory {
    id: number;
    boxCode: string;
    transactionType: string;
    quantity: number;
    notes: string;
    createdAt: string;
    stockAfterTransaction: number;
}

export async function getBoxes(): Promise<Box[]> {
    const response = await API.get("/boxes");
    return response.data;
}

export async function searchBoxes(model: string): Promise<Box[]> {
    const response = await API.get("/boxes/search", {
        params: { model },
    });
    return response.data;
}

export async function createBox(data: {
    boxCode: string;
    models: string[];
    quantity: number;
}): Promise<Box> {
    const response = await API.post("/boxes", data);
    return response.data;
}

export async function addStock(data: {
    boxCode: string;
    quantity: number;
    notes?: string;
}): Promise<StockResponse> {
    const response = await API.post("/boxes/add", data);
    return response.data;
}

export async function sellStock(data: {
    boxCode: string;
    quantity: number;
    notes?: string;
}): Promise<StockResponse> {
    const response = await API.post("/boxes/sell", data);
    return response.data;
}

export async function getHistory(
    boxCode: string
): Promise<StockHistory[]> {
    const response = await API.get(`/boxes/${boxCode}/history`);
    return response.data;
}