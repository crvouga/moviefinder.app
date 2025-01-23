(ns linkpage.frontend.ui.button
  (:require [linkpage.frontend.ui.icon :as icon]))

(def button-type->html-type
  {:button-type/submit "submit"
   :button-type/reset "reset"
   :button-type/button "button"})

(defn view [i]
  (let [label (-> i :button/label)
        type (-> i :button/type button-type->html-type (or "button"))
        loading? (-> i :button/loading? #_(or true))]
    [:button
     {:type type
      :disabled loading?
      :aria-busy loading?
      :class (str
              "bg-blue-500 text-white py-4 rounded flex items-center justify-center w-full font-bold text-lg relative "
              (when (not loading?)
                "hover:opacity-75 focus:opacity-75 active:opacity-75 ")
              (when loading?
                "cursor-wait opacity-75 "))}

     (when loading?
       [:div.absolute.inset-0.flex.items-center.justify-center
        (icon/spinner {:class "size-12 animate-spin"})])
     [:div {:class (when loading? "opacity-0")} label]]))