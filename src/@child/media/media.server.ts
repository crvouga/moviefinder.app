import { Server } from "../../@shared/server";
import { contract } from "./media.contract";

export const mediaRouter = ({ s }: { s: Server }) => {
  return s.router(contract, {
    mediaPage: async () => {
      return {
        status: 500,
        body: {
          error: "Not implemented",
        }
      }
    },

  });
}

