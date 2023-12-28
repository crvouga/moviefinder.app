import { drizzle } from 'drizzle-orm/postgres-js';
import postgres from 'postgres';
import * as schema from './schema';


const DATABASE_URL = process.env.DATABASE_URL

if(typeof DATABASE_URL !== 'string') {
    throw new Error("DATABASE_URL is not set")
}

export const connection = postgres(DATABASE_URL);

export const db = drizzle(connection, {
    schema
});

