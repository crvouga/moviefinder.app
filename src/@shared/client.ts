
import { initClient, AppRouter } from "@ts-rest/core";

const baseUrl = process.env.NODE_ENV === "production" ? "" : "http://localhost:8000";

export const createClient = <T extends AppRouter>(contract: T) => {
    return initClient(contract, {
        baseUrl,
        baseHeaders: {}
    })
} 

