(ns lib.http-cookie
  (:require [clojure.string :as str]))

(defn to-header-value
  [{:keys [cookie/name
           cookie/value
           cookie/max-age
           cookie/path
           cookie/domain
           cookie/secure
           cookie/http-only
           cookie/same-site]}]
  (let [base-pair (str name "=" value)
        parts (cond-> [base-pair]
                ;; Default path to "/"
                true (conj (str "Path=" (or path "/")))
                domain (conj (str "Domain=" domain))
                max-age (conj (str "Max-Age=" max-age))
                secure (conj "Secure")
                (not= false http-only) (conj "HttpOnly")
                same-site (conj (str "SameSite=" same-site)))]
    (str/join "; " parts)))