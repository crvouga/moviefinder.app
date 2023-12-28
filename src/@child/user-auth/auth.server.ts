import { Server } from '../../@shared/server';
import { contract } from './auth.contract';

export const authRouter = ({s}: {s: Server}) => {
  return s.router(contract, {
    loginWithEmailAndPassword: async (request) => {
      console.log(request.body.email)
      console.log(request.body.password)
      return {
        status: 200,
        body: {
          token: "123",
        }
      }
    },
  })
}

