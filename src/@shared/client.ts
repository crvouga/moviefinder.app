import {initClient} from "@ts-rest/core";
import {initQueryClient} from "@ts-rest/react-query";

export const createClient = (contract: Parameters<typeof initClient>[0])  => {
    return initClient(contract, {
      baseUrl: 'http://localhost:3000',
      baseHeaders: {},
    });
}


export const createQueryClient = (contract: Parameters<typeof initQueryClient>[0])  => {
    return initQueryClient(contract, {
      baseUrl: 'http://localhost:3000',
      baseHeaders: {},
    });
}