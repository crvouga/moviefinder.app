(ns app.media.media-db.inter
  (:require [app.media.media]))

(defmulti init :media-db/impl)
(defmulti query! :media-db/impl)
(defmulti put! :media-db/impl)

