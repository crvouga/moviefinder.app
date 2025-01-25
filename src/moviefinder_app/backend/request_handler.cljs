(ns moviefinder-app.backend.request-handler
  (:require [core.http-server.http-request :as http-request]))

(defmulti request-handler!
  (fn [req _res] (http-request/url req)))