(ns lib.words.name
  (:require [lib.words.adjectives :refer [adjectives]]
            [lib.words.nouns :refer [nouns]]))

(defn random []
  (str (rand-nth adjectives) " " (rand-nth nouns)))
