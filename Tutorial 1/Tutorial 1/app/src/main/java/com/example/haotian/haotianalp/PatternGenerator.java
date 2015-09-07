/*
Copyright 2010-2013 Michael Shick

This file is part of 'Lock Pattern Generator'.

'Lock Pattern Generator' is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version.

'Lock Pattern Generator' is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
'Lock Pattern Generator'.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.example.haotian.haotianalp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PatternGenerator
{
    protected int mGridLength;
    protected int mMinNodes;
    protected int mMaxNodes;
    protected Random mRng;
    protected List<Point> mAllNodes;
    // Used nodes array
    protected boolean[][] usedNodes;

    public PatternGenerator()
    {
        mRng = new Random();
        setGridLength(0);
        setMinNodes(0);
        setMaxNodes(0);
    }


    private boolean validateCandidate(Point currentPoint, Point candidatePoint)
    {
        // Is it a used node?
        if (usedNodes[candidatePoint.x][candidatePoint.y]) {
            return false;
        }

        int xShift = candidatePoint.x - currentPoint.x;
        int yShift = candidatePoint.y - currentPoint.y;

        //whether it's a 2-2
        int steps = computeGcd(Math.abs(xShift),Math.abs(yShift));
        int xStep = xShift/steps;
        int yStep = yShift/steps;

        //if anything in between is not used, invalidate the candidate
        for (int i = 1; i < steps; i++){
            if (!(usedNodes[currentPoint.x+xStep*i][currentPoint.y+yStep*i])){
                return false;
            }
        }

        //else, this point passes two tests
        return true;
    }

    private Point pointGenerator()
    {
        Point point = new Point(mRng.nextInt(3), mRng.nextInt(3));
        return point;
    }

    public List<Point> getPattern()
    {
        // Instantiate list for nodes
        mAllNodes = new ArrayList<Point>();

        // Maps used nodes
        usedNodes = new boolean[mGridLength][mGridLength];

        // Mark everything as available
        for (int i = 3; i<3; i++) {
            for (int j = 0; j < 3; j++) {
                usedNodes[i][j] = false;
            }
        }

        // Add initial point
        mAllNodes.add(pointGenerator());
        usedNodes[mAllNodes.get(0).x][mAllNodes.get(0).y] = true;
        int pCount = 1;
        // Determine pattern length
        int length = 3 + mRng.nextInt(3);

        // Generate other pattern nodes
        while(pCount < length){
            Point currentPoint = mAllNodes.get(pCount - 1);
            Point candidatePoint = pointGenerator();
            if(validateCandidate(currentPoint, candidatePoint)){
                mAllNodes.add(pCount, candidatePoint);
                usedNodes[candidatePoint.x][candidatePoint.y] = true;
                pCount++;
            }
        }

        return mAllNodes;
    }

    //
    // Accessors / Mutators
    //

    public void setGridLength(int length)
    {
        // build the prototype set to copy from later
        List<Point> allNodes = new ArrayList<Point>();
        for(int y = 0; y < length; y++)
        {
            for(int x = 0; x < length; x++)
            {
                allNodes.add(new Point(x,y));
            }
        }
        mAllNodes = allNodes;

        mGridLength = length;
    }
    public int getGridLength()
    {
        return mGridLength;
    }

    public void setMinNodes(int nodes)
    {
        mMinNodes = nodes;
    }
    public int getMinNodes()
    {
        return mMinNodes;
    }

    public void setMaxNodes(int nodes)
    {
        mMaxNodes = nodes;
    }
    public int getMaxNodes()
    {
        return mMaxNodes;
    }

    //
    // Helper methods
    //

    public static int computeGcd(int a, int b)
    /* Implementation taken from
     * http://en.literateprograms.org/Euclidean_algorithm_(Java)
     * Accessed on 12/28/10
     */
    {
        if(b > a)
        {
            int temp = a;
            a = b;
            b = temp;
        }

        while(b != 0)
        {
            int m = a % b;
            a = b;
            b = m;
        }

        return a;
    }
}
