export type AuthInput = {
  t: "login";
  email: string;
  password: string;
};

export type AuthOutput =
  | {
      t: "logged-in-succeeded";
      result: {
        token: string;
      };
    }
  | {
      t: "logged-in-failed";
    };

export const create = ({ input, output }: { input: any; output: any }) => {
  input.on("login", async (data) => {
    await output.emit("logged-in", data);
  });
};
