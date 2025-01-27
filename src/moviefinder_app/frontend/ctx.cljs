(ns moviefinder-app.frontend.ctx)

(def backend-url-dev "http://localhost:5002")
(def backend-url-prod "")
(defn dev? []
  (try
    (= (.-hostname js/window.location) "localhost")
    (catch js/Error _ false)))
(def backend-url (if (dev?) backend-url-dev backend-url-prod))

(def ctx {:wire/backend-url backend-url
          :verify-sms/impl :verify-sms-impl/fake
          :media-db/impl :media-db-impl/rpc
          :sql/impl :sql-impl/pglite})
