CREATE TABLE IF NOT EXISTS access_log (
  access_date_time TIMESTAMP WITH TIME ZONE,
  ip VARCHAR(15),
  username VARCHAR(50)
);
