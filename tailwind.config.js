/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.rs", "./public/**/*.{html,js,css,svg}"],
  theme: {
    extend: {
      spacing: {
        bar: "5rem",
      },
      colors: {
        placeholder: "#a3a3a3",
        skeleton: "#3f3f46",
      },
      borderColor: {
        DEFAULT: "#3f3f46",
      },
      textColor: {
        secondary: "#d4d4d8",
        muted: "#a3a3a3",
      },
      opacity: {
        active: "0.5",
      },
    },
  },
  plugins: [],
};
