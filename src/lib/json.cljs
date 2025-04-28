(ns lib.json)

(defn json->clj [body]
  (try
    (when (string? body)
      (-> body
          js/JSON.parse
          (js->clj :keywordize-keys true)))
    (catch :default _e
      nil)))