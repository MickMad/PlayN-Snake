/**
 * Copyright (C) 2012 Michele Perla (the.mickmad@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package mick.core;

import playn.core.ImageLayer;
import playn.core.GroupLayer;

public class Entity {
	private ImageLayer image;
	private GroupLayer parent;
	private float x, y;
	public Entity(ImageLayer image, GroupLayer parent, float x, float y){
		this.image = image;
		this.parent=parent;
		this.x=x;
		this.y=y;
		parent.add(image);
		this.setTranslation(x, y);
	}
	public void setTranslation(float x, float y){
		this.x=x;
		this.y=y;
		image.setTranslation(x,y);
	}
	public GroupLayer parent(){
		return parent;
	}
	public ImageLayer image(){
		return image;
	}
	public float getTranslationX(){
		return x;
	}
	public float getTranslationY(){
		return y;
	}
}
