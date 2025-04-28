(ns lib.kv.interface)

(defmulti new!
  "Returns a new kv store on a channel"
  :kv/impl)

(defmulti get!
  "Returns the value of a key on a channel"
  :kv/impl)

(defmulti set!
  "Sets the value of a key on a channel"
  :kv/impl)

(defmulti zap!
  "Zaps the value of a key on a channel"
  :kv/impl)



