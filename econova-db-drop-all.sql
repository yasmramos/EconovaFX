-- drop all foreign keys
alter table accounts drop constraint if exists fk_accounts_parent_account_id;
drop index if exists ix_accounts_parent_account_id;

alter table transactions drop constraint if exists fk_transactions_created_by;
drop index if exists ix_transactions_created_by;

alter table transaction_entries drop constraint if exists fk_transaction_entries_transaction_id;
drop index if exists ix_transaction_entries_transaction_id;

alter table transaction_entries drop constraint if exists fk_transaction_entries_account_id;
drop index if exists ix_transaction_entries_account_id;

-- drop all
drop table if exists accounts;

drop table if exists transactions;

drop table if exists transaction_entries;

drop table if exists users;

