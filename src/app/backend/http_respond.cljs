(ns app.backend.http-respond
  (:require [lib.http-server.http-request :as http-request]))

(defmulti http-respond!
  (fn [req _res] (http-request/url req)))