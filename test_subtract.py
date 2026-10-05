import unittest

from subtract import subtract


class SubtractTest(unittest.TestCase):
    def test_positive_integers(self):
        self.assertEqual(subtract(5, 3), 2)

    def test_negative_result(self):
        self.assertEqual(subtract(3, 5), -2)

    def test_floats(self):
        self.assertAlmostEqual(subtract(1.5, 0.25), 1.25)


if __name__ == "__main__":
    unittest.main()
