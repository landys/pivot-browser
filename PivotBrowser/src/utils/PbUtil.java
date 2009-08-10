package utils;

import java.util.List;

public class PbUtil
{
    public static String getUriName(final String uri)
    {
        if (uri == null || uri.length() == 0)
        {
            return "";
        }

        int i = uri.lastIndexOf(".");
        if (i == -1)
        {
            i = uri.length();
        }

        int j = uri.lastIndexOf("/", i - 1);

        return uri.substring(j + 1, i);
    }

    public static int parseInt(final String s)
    {
        int re = 0;
        try
        {
            re = Integer.parseInt(s.trim());
        }
        catch (Exception e)
        {
            // do nothing
        }

        return re;
    }

    public static long parseLong(final String s)
    {
        long re = 0;
        try
        {
            re = Long.parseLong(s.trim());
        }
        catch (Exception e)
        {
            // do nothing
        }

        return re;
    }

    /**
     * Merge as "s1:s2:s3:...".
     * 
     * @param ss
     * @param deli
     * @return
     */
    public static String mergeStrings(final List<String> ss, final char deli)
    {
        final StringBuilder sb = new StringBuilder();
        for (String s : ss)
        {
            if (sb.length() > 0)
            {
                sb.append(deli);
            }
            sb.append(s);
        }

        return sb.toString();
    }
}
