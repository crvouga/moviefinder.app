(ns linkpage.backend.request-handler
  (:require [linkpage.core.http-server.http-request :as http-request]))

(defmulti request-handler!
  (fn [req _res] (http-request/url req)))