(ns linkpage.frontend.ui.form)

(defn view [props & children]
  [:form.flex.flex-col.w-full.gap-6
   (merge props
          {:on-submit (-> props :form/on-submit)})
   (for [c children]
     ^{:key c} c)])
  