## Using Postgres as key-value cache 

Sometines, we can’t use Redis or Memcached for caching pupose. One reason is license problem and that technology hasn’t approved to be used inside your company. Instead, we need to design a solution that use existing infrastructure like PostgreSQL database.

Note: Using ChatGPT to plan the idea

Core features
We need to design a caching solution that satisfy these constrains:

Using Postgres
Support TimeToLive or Expired time
Support Least Recent Used (LRU) behaviour. We can active refresh cache that frequently used and/optional delete old cached
Support unique refresh for key if the application running on multiple instances.
Using background refresh job to simplify the app logic

For more detail: https://nhatvu.dev/technical/96-poc-key-value-cache-with-postgres