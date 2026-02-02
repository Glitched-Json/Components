//$Vertex Shader
#version 330

layout (location = 0) in vec3 vPos; //$ Position

uniform mat4 view;
uniform mat4 projection;
uniform mat4 transform;

void main() {
    gl_Position = projection * view * transform * vec4(vPos, 1.);
}

//$Fragment Shader
#version 330

uniform vec3 fCol; //$ colorID

void main() {
    gl_FragColor = vec4(fCol, 1.);
}