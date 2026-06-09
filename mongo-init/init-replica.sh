#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Script d'initialisation du Replica Set MongoDB
# Architecture : 3 Config Servers + 3 Shards + 1 Router (mongos)
#
# Déclenchement : automatique via docker-compose healthcheck
# ─────────────────────────────────────────────────────────────────────────────

set -e

echo "⏳  Attente de la disponibilité de mongo1..."
sleep 10

echo "🔧  Initialisation du Replica Set rs0..."

mongosh --host mongo1:27017 --eval "
rs.initiate({
  _id: 'rs0',
  members: [
    { _id: 0, host: 'mongo1:27017', priority: 2 },
    { _id: 1, host: 'mongo2:27017', priority: 1 },
    { _id: 2, host: 'mongo3:27017', priority: 1 }
  ]
});
"

echo "⏳  Attente de l'élection du PRIMARY..."
sleep 15

echo "🗄️   Création de la base de données et des index..."

mongosh --host mongo1:27017 --eval "
use afric_banking;

// ─── Index Collection users ───────────────────────────────────────────────
db.users.createIndex({ email: 1 }, { unique: true, name: 'idx_users_email_unique' });
db.users.createIndex({ createdAt: -1 }, { name: 'idx_users_createdAt' });

// ─── Index Collection account ─────────────────────────────────────────────
db.account.createIndex({ userId: 1 }, { unique: true, name: 'idx_account_userId_unique' });
db.account.createIndex({ accountNumber: 1 }, { unique: true, name: 'idx_account_number_unique' });

// ─── Index Collection accounting_journal ──────────────────────────────────
db.accounting_journal.createIndex({ accountId: 1, createdAt: -1 }, { name: 'idx_journal_accountId_date' });
db.accounting_journal.createIndex({ direction: 1 }, { name: 'idx_journal_direction' });
db.accounting_journal.createIndex({ createdAt: -1 }, { name: 'idx_journal_createdAt' });

print('✅  Base de données et index créés avec succès.');
print('📊  Replica Set status :');
rs.status().members.forEach(m => print('  - ' + m.name + ' : ' + m.stateStr));
"

echo "✅  Initialisation MongoDB terminée !"
