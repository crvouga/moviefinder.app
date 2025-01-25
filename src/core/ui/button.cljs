(ns core.ui.button
  (:require [core.ui.icon :as icon]))

(def button-type->html-type
  {:button-type/submit "submit"
   :button-type/reset "reset"
   :button-type/button "button"})

(defn view [i]
  (let [label (-> i :button/label)
        full? (-> i :button/full? (or false))
        type (-> i :button/type button-type->html-type (or "button"))
        on-click (-> i :button/on-click)
        loading? (-> i :button/loading? #_(or true))]
    [:button
     {:type type
      :disabled loading?
      :on-click on-click
      :aria-busy loading?
      :class (str
              "bg-blue-500 text-white py-3 px-4 rounded flex items-center justify-center font-bold text-lg relative cursor-pointer "
              (if full? "w-full " "w-fit ")
              (when (not loading?)
                "hover:opacity-75 focus:opacity-75 active:opacity-75 ")
              (when loading?
                "cursor-wait opacity-75 "))}

     (when loading?
       [:div.absolute.inset-0.flex.items-center.justify-center
        (icon/spinner {:class "size-10 animate-spin"})])
     [:div {:class (when loading? "opacity-0")} label]]))