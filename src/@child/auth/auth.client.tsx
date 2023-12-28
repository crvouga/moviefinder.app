import { useMutation } from '@tanstack/react-query';
import { createClient } from '../../@shared/client';
import { contract } from './auth.contract';

const client = createClient(contract)


export const LoginPage = () => {
  const loginMutation = useMutation({
    mutationFn: async () => {
      return await client.loginWithEmailAndPassword({
        body: {
          email: 'email',
          password: 'password',
        }
      })
    }
  })


  return (
    <div>
      <button onClick={() => loginMutation.mutate()}>Login</button>
      {loginMutation.isPending && <div>Loading...</div>}
    </div>   
  )
}