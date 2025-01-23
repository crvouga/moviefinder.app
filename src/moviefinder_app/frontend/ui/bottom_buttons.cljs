(ns moviefinder-app.frontend.ui.bottom-buttons)


(defn view [i]
  [:div.flex.w-full.items-center.h-16
   (for [button (-> i :bottom-buttons/buttons)
         :let [on-click (-> button  :bottom-button/on-click)
               label (-> button :bottom-button/label)
               view-icon (-> button :bottom-button/view-icon)]]
     ^{:key label}
     [:button {:on-click on-click
               :class "flex flex-col items-center justify-center flex-1 h-full text-base text-sm gap-1 cursor-pointer"}
      (when view-icon
        [view-icon {:class "size-8 flex items-center justify-center"}])
      [:p label]])])