
import { initClient, AppRouter } from "@ts-rest/core";

export const createClient = <T extends AppRouter>(contract: T) => {
    return initClient(contract, {
        baseUrl: "http://localhost:3000",
        baseHeaders: {}
    })
} 

