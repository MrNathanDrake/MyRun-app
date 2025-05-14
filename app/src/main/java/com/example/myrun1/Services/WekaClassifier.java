package com.example.myrun1.Services;

class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.Naadf4460(i);
        return p;
    }
    static double Naadf4460(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() <= 0.532558) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() > 0.532558) {
            p = WekaClassifier.N3370cc0d1(i);
        }
        return p;
    }
    static double N3370cc0d1(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 200.913023) {
            p = WekaClassifier.Ndd3c7972(i);
        } else if (((Double) i[0]).doubleValue() > 200.913023) {
            p = 2;
        }
        return p;
    }
    static double Ndd3c7972(Object []i) {
        double p = Double.NaN;
        if (i[7] == null) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() <= 7.872133) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() > 7.872133) {
            p = 2;
        }
        return p;
    }
}