INSERT INTO game (id, name, description)
VALUES ('slot-machine', 'Slot Machine', 'Simple 3-reel slot machine')
ON CONFLICT (id) DO NOTHING;