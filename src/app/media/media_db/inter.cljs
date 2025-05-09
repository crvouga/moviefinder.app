(ns app.media.media-db.inter
  (:require [app.media.media]))

(defmulti init (fn [inst _] (:media-db/impl inst)))

(defmulti query! (fn [inst _] (:media-db/impl inst)))

(defmulti put! (fn [inst _] (:media-db/impl inst)))

