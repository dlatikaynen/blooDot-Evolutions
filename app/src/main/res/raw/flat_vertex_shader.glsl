#version 300 es

uniform mat4 uMVPMatrix;
in vec4 aPosition;

void main() {
    gl_Position = uMVPMatrix * aPosition;
}
