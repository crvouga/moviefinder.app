use std::collections::HashMap;

#[derive(Debug, Eq, PartialEq)]
pub struct QueryParams(HashMap<String, String>);

impl QueryParams {
    pub fn is_empty(&self) -> bool {
        self.0.is_empty()
    }

    pub fn empty() -> QueryParams {
        QueryParams(HashMap::new())
    }

    pub fn to_string(&self) -> String {
        self.0
            .iter()
            .map(|(key, value)| format!("{}={}", key, value))
            .collect::<Vec<String>>()
            .join("&")
    }
}

impl From<HashMap<String, String>> for QueryParams {
    fn from(query_params: HashMap<String, String>) -> QueryParams {
        QueryParams(query_params)
    }
}

impl From<&str> for QueryParams {
    fn from(query_string: &str) -> QueryParams {
        query_string
            .split('&')
            .map(|pair| {
                let mut parts = pair.split('=');
                let key = parts.next().unwrap_or("").to_string();
                let value = parts.next().unwrap_or("").to_string();
                (key, value)
            })
            .collect::<HashMap<String, String>>()
            .into()
    }
}

impl Default for QueryParams {
    fn default() -> Self {
        QueryParams(HashMap::new())
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn test_from_string() {
        let query_string = "name=John&age=20";
        let query_params = QueryParams::from(query_string);
        let expected = QueryParams(
            [
                ("name".to_string(), "John".to_string()),
                ("age".to_string(), "20".to_string()),
            ]
            .iter()
            .cloned()
            .collect(),
        );
        assert_eq!(query_params, expected);
    }
}
