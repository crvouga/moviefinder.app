(ns moviefinder-app.frontend.db-conn-sql
  (:require
   [moviefinder-app.ctx :refer [ctx]]
   [moviefinder-app.core.db-conn-sql.impl]
   [moviefinder-app.core.db-conn-sql.interface :as db-conn-sql]))


(def db-conn (db-conn-sql/new ctx))