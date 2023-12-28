import { Server } from "../../@shared/server";
import { contract } from "./feed.contract";

export const feedRouter = ({s}: {s: Server}) => {
 return s.router(contract, {
    getPost: async () => {
      return {
        status: 200,
        body: {
          body: 'body',
          id: 'id',
          title: 'title',
        },
      };
    },

    createPost: async () => {
      return {
        status: 201,
        body: {
          body: 'body',
          id: 'id',
          title: 'title',
        },
      };
    },
  });
}

