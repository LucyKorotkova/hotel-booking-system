INSERT INTO hotels (id, name, address) VALUES (1, 'Grand Hotel', 'Central Street 1');
INSERT INTO hotels (id, name, address) VALUES (2, 'Sea View Resort', 'Beach Road 42');

INSERT INTO rooms (id, hotel_id, number, available, times_booked, version) VALUES (1, 1, '101', true, 5, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked, version) VALUES (2, 1, '102', true, 2, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked, version) VALUES (3, 1, '103', true, 0, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked, version) VALUES (4, 2, '201', true, 1, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked, version) VALUES (5, 2, '202', true, 3, 0);