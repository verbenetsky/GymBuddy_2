module.exports = {
  env: {
    es6: true,
    node: true,
  },
  parserOptions: {
    ecmaVersion: 2018,
  },
  extends: [
    "eslint:recommended",
    "google",
  ],
  rules: {
    // Wyłączanie reguły dla spacji w nawiasach klamrowych
    "object-curly-spacing": "off",
    // Wyłączanie reguły dla końcowych spacji
    "no-trailing-spaces": "off",
    // Wyłączanie reguły dla wielokrotnych spacji
    "no-multi-spaces": "off",
    // Wyłączanie reguły dla maksymalnej długości linii
    "max-len": "off",
    // Wyłączanie reguły dla nieużywanych zmiennych
    "no-unused-vars": "off",
    "prefer-arrow-callback": "error",
    "quotes": ["error", "double", { "allowTemplateLiterals": true }],
    "no-restricted-globals": ["error", "name", "length"],
  },
  overrides: [
    {
      files: ["**/*.spec.*"],
      env: {
        mocha: true,
      },
      rules: {},
      globals: {},
    },
  ],
};
