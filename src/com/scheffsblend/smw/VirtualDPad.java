/****************************************************************************

    This file is part of Doom-for-Android.

    Doom-for-Android is free software: you can redistribute it and/or
    modify it under the terms of the GNU General Public License as published
    by the Free Software Foundation version 1 of the License.

    Doom-for-Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Doom-for-Android. If not, see http://www.gnu.org/licenses
    
****************************************************************************/

package com.scheffsblend.smw;

/**
 * Doom for Android Virtual D-Pad
 * @author cscheff
 *
 */
public class VirtualDPad {
	public final static int POS_CENTER 		= 0x00;	// 0000
	public final static int POS_UP 			= 0x01;	// 0001
	public final static int POS_RIGHT		= 0x02;	// 0010
	public final static int POS_DOWN		= 0x04;	// 0100
	public final static int POS_LEFT		= 0x08;	// 1000
	public final static int POS_UP_RIGHT	= 0x03;	// 0011
	public final static int POS_DOWN_RIGHT	= 0x06;	// 0110
	public final static int POS_UP_LEFT		= 0x09;	// 1001
	public final static int POS_DOWN_LEFT	= 0x0C;	// 1100
	
	// width of the virtual stick area
	public int mWidth = 0;
	// height of the virtual stick area
	public int mHeight = 0;
	// center area needs a bit of a deadzone, store that value here
	public int mDeadzone = 0;
	
	public void VirtualSitck() {
		mWidth = 0;
		mHeight = 0;
		mDeadzone = 0;
	}
	
	public VirtualDPad(int w, int h, int d) {
		mWidth = w;
		mHeight = h;
		mDeadzone = d;
	}
	
	// Hopefully the values chosen for the constants up above make some sense here.
	// I've used some boolean algebra to set the proper bits.  Sorry, that's the 
	// embedded coding side of me! :P
	public int getPosition(float x, float y) {
		int position = POS_CENTER;
		if(x < (mWidth/2 - mDeadzone))
			position |= POS_LEFT;
		else if(x > (mWidth/2 + mDeadzone))
			position |= POS_RIGHT;
		
		if(y < (mHeight/2 - mDeadzone))
			position |= POS_UP;
		else if(y > (mHeight/2 + mDeadzone))
			position |= POS_DOWN;
		
		return position;
	}
}
