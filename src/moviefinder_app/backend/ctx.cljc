(ns moviefinder-app.backend.ctx)

(def ctx {:verify-sms/impl :verify-sms-impl/fake
          :sql/impl :sql-impl/pglite})
