(ns moviefinder-app.frontend.config)

(def backend-url-dev "http://localhost:8888")
(def backend-url-prod "")
(defn dev? []
  (try
    (= (.-hostname js/window.location) "localhost")
    (catch js/Error _ false)))
(def backend-url (if (dev?) backend-url-dev backend-url-prod))

(def config {:wire/backend-url backend-url
             :verify-sms/impl :verify-sms-impl/fake
             :media-db/impl :media-db-impl/rpc
             :sql/impl :sql-impl/pglite})
