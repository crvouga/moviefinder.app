export const create = ({ input, output }: { input: any; output: any }) => {
  input.on("login-with-email-password", (payload) => {
    const { email, password } = event.data;
      const { success, error } = output;


      output.emit('success', 'success)

  });
};
