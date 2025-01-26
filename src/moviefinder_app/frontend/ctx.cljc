(ns moviefinder-app.frontend.ctx)

(def ctx {:verify-sms/impl :verify-sms-impl/fake
          :media-db/impl :media-db-impl/tmdb-api
          :sql/impl :sql-impl/pglite})
