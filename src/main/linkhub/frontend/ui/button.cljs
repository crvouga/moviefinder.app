(ns linkhub.frontend.ui.button)

(def button-type->html-type
  {:button-type/submit "submit"
   :button-type/reset "reset"
   :button-type/button "button"})

(defn view [i]
  (let [label (-> i :button/label)
        type (-> i :button/type button-type->html-type (or "button"))
        loading? (-> i :button/loading?)]
   [:button
    {:type type 
     :disabled loading?
     :aria-busy loading?}
    label]))