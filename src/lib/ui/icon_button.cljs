(ns lib.ui.icon-button)

(defn view [i]
  (let [view-icon (-> i :icon-button/view-icon)
        on-click (-> i :icon-button/on-click)]
    [:button.size-8.rounded-full.text-white.cursor-pointer
     {:on-click on-click}
     (when view-icon
       [view-icon {:class "size-7"}])]))