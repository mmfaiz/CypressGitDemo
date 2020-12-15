package com.matchi

class LuhnValidator {

    private static String generat2e(String s) {
        int digit = 10 - doLuhn(s, true) % 10;
        return "" + digit;
    }

    public static def generate(String idWithoutCheckdigit) {
        int sum = 0;

        for (int i = 0; i < idWithoutCheckdigit.length(); i++) {

            char ch = idWithoutCheckdigit
                    .charAt(idWithoutCheckdigit.length() - i - 1);

            int digit = (int)ch - 48;

            int weight;
            if (i % 2 == 0) {

                weight = (2 * digit) - (int) (digit / 5) * 9;

            } else {
                weight = digit;
            }

            sum += weight;
        }

        sum = Math.abs(sum) + 10;

        return (10 - (sum % 10)) % 10;

    }

    private static int doLuhn(String s, boolean evenPosition) {
        int sum = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(s.substring(i, i + 1));
            if (evenPosition) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            evenPosition = !evenPosition;
        }

        return sum;
    }

    public static boolean validate(String number) {
        int sum = 0
        boolean alternate = false
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1))
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    public static boolean validateGender(def number, User.Gender gender) {
        if (gender.equals(User.Gender.male)) {
            return (number % 2 == 1)
        } else if (gender.equals(User.Gender.female)) {
            return (number % 2 == 0)
        }

        return false
    }

    public static boolean validateType(def number, Customer.CustomerType type) {
        if (type.equals(Customer.CustomerType.MALE)) {
            return (number % 2 == 1)
        } else if (type.equals(Customer.CustomerType.FEMALE)) {
            return (number % 2 == 0)
        }

        return false
    }
}
