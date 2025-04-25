(ns core.ui.form)

(defn- concatv [v1 v2]
  (vec (concat v1 v2)))

(defn view [{:keys [on-submit] :as props :or {on-submit identity}} & children]
  (concatv
   [:form (merge props {:on-submit #(do (.preventDefault %) (on-submit))})]
   children))
  