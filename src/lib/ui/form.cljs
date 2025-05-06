(ns lib.ui.form
  (:require
   [lib.ui.children :as children]))

(defn view [{:keys [:form/on-submit] :as props :or {on-submit identity}} & children]
  (children/with
   [:form (merge props {:on-submit #(do (.preventDefault %) (on-submit))})]
   children))
  