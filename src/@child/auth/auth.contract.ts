import { initContract } from '@ts-rest/core';
import { z } from 'zod';

const c = initContract();



export const contract = c.router({
  loginWithEmailAndPassword: {
    method: "POST",
    path: "/login-with-email-and-password",
    body: z.object({
      email: z.string(),
      password: z.string(),
    }),
    responses:{
        200: z.object({
            token: z.string(),
        }),
        401: z.object({
            error: z.string(),
        }),
    }
  },
});

