(ns moviefinder-app.ctx)

(def ctx {:verify-sms/impl :verify-sms-impl/fake
          :db-conn-sql/impl :db-conn-sql-impl/pglite})
